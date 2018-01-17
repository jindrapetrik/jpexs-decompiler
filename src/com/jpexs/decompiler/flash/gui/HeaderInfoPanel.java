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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.gui.helpers.TableLayoutHelper;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.event.ChangeEvent;
import layout.TableLayout;

/**
 *
 * @author JPEXS
 */
public class HeaderInfoPanel extends JPanel implements TagEditorPanel {

    private final JLabel signatureLabel = new JLabel();

    private final JLabel compressionLabel = new JLabel();

    private final JLabel gfxLabel = new JLabel();

    private final JLabel versionLabel = new JLabel();

    private final JLabel fileSizeLabel = new JLabel();

    private final JLabel frameRateLabel = new JLabel();

    private final JLabel frameCountLabel = new JLabel();

    private final JLabel displayRectTwipsLabel = new JLabel();

    private final JLabel displayRectPixelsLabel = new JLabel();

    private final JPanel compressionEditorPanel = new JPanel();

    private final JComboBox<ComboBoxItem<SWFCompression>> compressionComboBox = new JComboBox<>();

    private final JPanel versionEditorPanel = new JPanel();

    private final JSpinner versionEditor = new JSpinner();

    private final JCheckBox gfxCheckBox = new JCheckBox();

    private final JPanel frameRateEditorPanel = new JPanel();

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

    private final JPanel warningPanel = new JPanel();

    private final JLabel warningLabel = new JLabel();

    private SWF swf;

    public HeaderInfoPanel() {
        setLayout(new BorderLayout());

        TableLayout tl;
        propertiesPanel.setLayout(tl = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED}
        }));

        FlowLayout layout = new FlowLayout(SwingConstants.WEST);
        layout.setHgap(0);
        layout.setVgap(0);

        compressionEditorPanel.setLayout(layout);
        compressionComboBox.addItem(new ComboBoxItem<>(AppStrings.translate("header.uncompressed"), SWFCompression.NONE));
        compressionComboBox.addItem(new ComboBoxItem<>("Zlib", SWFCompression.ZLIB));
        compressionComboBox.addItem(new ComboBoxItem<>("LZMA", SWFCompression.LZMA));
        compressionComboBox.addActionListener((ActionEvent e) -> {
            validateHeader();
        });
        compressionEditorPanel.add(compressionComboBox);

        versionEditorPanel.setLayout(layout);
        versionEditor.setPreferredSize(new Dimension(80, versionEditor.getPreferredSize().height));
        versionEditor.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });
        versionEditorPanel.add(versionEditor);

        gfxCheckBox.addActionListener((ActionEvent e) -> {
            validateHeader();
        });

        frameRateEditorPanel.setLayout(layout);
        frameRateEditor.setPreferredSize(new Dimension(80, frameRateEditor.getPreferredSize().height));
        frameRateEditorPanel.add(frameRateEditor);

        displayRectEditorPanel.setLayout(layout);
        displayRectEditorPanel.setMinimumSize(new Dimension(10, displayRectEditorPanel.getMinimumSize().height));
        xMinEditor.setPreferredSize(new Dimension(80, xMinEditor.getPreferredSize().height));
        xMaxEditor.setPreferredSize(new Dimension(80, xMaxEditor.getPreferredSize().height));
        yMinEditor.setPreferredSize(new Dimension(80, yMinEditor.getPreferredSize().height));
        yMaxEditor.setPreferredSize(new Dimension(80, yMaxEditor.getPreferredSize().height));
        displayRectEditorPanel.add(xMinEditor);
        displayRectEditorPanel.add(new JLabel(","));
        displayRectEditorPanel.add(yMinEditor);
        displayRectEditorPanel.add(new JLabel(" => "));
        displayRectEditorPanel.add(xMaxEditor);
        displayRectEditorPanel.add(new JLabel(","));
        displayRectEditorPanel.add(yMaxEditor);
        displayRectEditorPanel.add(new JLabel(" twips"));

        warningLabel.setIcon(View.getIcon("warning16"));
        warningPanel.setLayout(layout);
        warningPanel.setBackground(new Color(255, 213, 29));
        warningPanel.add(warningLabel);

        propertiesPanel.add(new JLabel(AppStrings.translate("header.signature")), "0,0");
        propertiesPanel.add(signatureLabel, "1,0");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.compression")), "0,1");
        propertiesPanel.add(compressionLabel, "1,1");
        propertiesPanel.add(compressionEditorPanel, "1,1");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.version")), "0,2");
        propertiesPanel.add(versionLabel, "1,2");
        propertiesPanel.add(versionEditorPanel, "1,2");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.gfx")), "0,3");
        propertiesPanel.add(gfxLabel, "1,3");
        propertiesPanel.add(gfxCheckBox, "1,3");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.filesize")), "0,4");
        propertiesPanel.add(fileSizeLabel, "1,4");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framerate")), "0,5");
        propertiesPanel.add(frameRateLabel, "1,5");
        propertiesPanel.add(frameRateEditorPanel, "1,5");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framecount")), "0,6");
        propertiesPanel.add(frameCountLabel, "1,6");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.displayrect")), "0,7");
        propertiesPanel.add(displayRectTwipsLabel, "1,7");
        propertiesPanel.add(displayRectEditorPanel, "1,7");
        propertiesPanel.add(new JLabel(""), "0,8");
        propertiesPanel.add(displayRectPixelsLabel, "1,8");
        propertiesPanel.add(warningPanel, "0,9,1,9");

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

    private int getVersionNumber() {
        return (int) versionEditor.getModel().getValue();
    }

    private SWFCompression getCompression() {
        @SuppressWarnings("unchecked")
        ComboBoxItem<SWFCompression> item = (ComboBoxItem<SWFCompression>) compressionComboBox.getSelectedItem();
        return item.getValue();
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        setEditMode(true);
    }

    private void saveButtonActionPerformed(ActionEvent evt) {
        swf.compression = getCompression();
        swf.version = getVersionNumber();
        swf.gfx = gfxCheckBox.isSelected();
        swf.frameRate = ((Number) (frameRateEditor.getModel().getValue())).floatValue();
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
        signatureLabel.setText(new String(swf.getHeaderBytes()));
        switch (swf.compression) {
            case LZMA:
                compressionLabel.setText(AppStrings.translate("header.compression.lzma"));
                compressionComboBox.setSelectedIndex(2);
                break;
            case ZLIB:
                compressionLabel.setText(AppStrings.translate("header.compression.zlib"));
                compressionComboBox.setSelectedIndex(1);
                break;
            case NONE:
                compressionLabel.setText(AppStrings.translate("header.compression.none"));
                compressionComboBox.setSelectedIndex(0);
                break;
        }

        versionLabel.setText(Integer.toString(swf.version));
        versionEditor.setModel(new SpinnerNumberModel(swf.version, 1, SWF.MAX_VERSION, 1));

        gfxLabel.setText(swf.gfx ? AppStrings.translate("yes") : AppStrings.translate("no"));
        gfxCheckBox.setSelected(swf.gfx);

        fileSizeLabel.setText(Long.toString(swf.fileSize));

        frameRateLabel.setText(Float.toString(swf.frameRate));
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
        String r = Double.toString(d);
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }

        return r;
    }

    private void setEditMode(boolean edit) {
        compressionLabel.setVisible(!edit);
        compressionEditorPanel.setVisible(edit);
        versionLabel.setVisible(!edit);
        versionEditorPanel.setVisible(edit);
        gfxLabel.setVisible(!edit);
        gfxCheckBox.setVisible(edit);
        frameRateLabel.setVisible(!edit);
        frameRateEditorPanel.setVisible(edit);

        displayRectTwipsLabel.setVisible(!edit);
        displayRectPixelsLabel.setVisible(!edit);
        displayRectEditorPanel.setVisible(edit);

        warningPanel.setVisible(false);

        editButton.setVisible(!edit);
        saveButton.setVisible(edit);
        cancelButton.setVisible(edit);
    }

    private boolean validateHeader() {
        int version = getVersionNumber();
        boolean gfx = gfxCheckBox.isSelected();
        SWFCompression compression = getCompression();
        String resultStr = "";
        boolean result = true;
        if (gfx && !(compression == SWFCompression.NONE || compression == SWFCompression.ZLIB)) {
            resultStr += AppStrings.translate("header.warning.unsupportedGfxCompression") + " ";
            result = false;
        }

        if (compression == SWFCompression.ZLIB && version < 6) {
            resultStr += AppStrings.translate("header.warning.minimumZlibVersion") + " ";
            result = false;
        }

        if (compression == SWFCompression.LZMA && version < 13) {
            resultStr += AppStrings.translate("header.warning.minimumLzmaVersion") + " ";
            result = false;
        }

        warningPanel.setVisible(!result);
        if (!result) {
            warningLabel.setText(resultStr);
        }

        return result;
    }

    @Override
    public boolean tryAutoSave() {
        // todo: implement
        return false;
    }

    @Override
    public boolean isEditing() {
        return saveButton.isVisible();
    }
}
