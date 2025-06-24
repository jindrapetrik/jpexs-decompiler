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
import com.jpexs.decompiler.flash.SWFCompression;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.helpers.TableLayoutHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import layout.TableLayout;

/**
 * @author JPEXS
 */
public class HeaderInfoPanel extends JPanel implements TagEditorPanel {

    private final JLabel signatureLabel = new JLabel();

    private final JLabel compressionLabel = new JLabel();

    private final JLabel gfxLabel = new JLabel();

    private final JLabel encryptedLabel = new JLabel();

    private final JLabel versionLabel = new JLabel();

    private final JLabel fileSizeLabel = new JLabel();

    private final JLabel frameRateLabel = new JLabel();

    private final JLabel frameCountLabel = new JLabel();

    private final JSpinner frameCountEditor = new JSpinner();

    private final JPanel frameCountEditorPanel = new JPanel();

    private final JLabel displayRectTwipsLabel = new JLabel();

    private final JLabel displayRectPixelsLabel = new JLabel();

    private final JPanel compressionEditorPanel = new JPanel();

    private final JComboBox<ComboBoxItem<SWFCompression>> compressionComboBox = new JComboBox<>();

    private final JPanel versionEditorPanel = new JPanel();

    private final JSpinner versionEditor = new JSpinner();

    private final JCheckBox gfxCheckBox = new JCheckBox();

    private final JCheckBox encryptedCheckBox = new JCheckBox();

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

    private JComboBox<String> unitComboBox;

    private final int UNIT_PIXELS = 0;
    private final int UNIT_TWIPS = 1;

    private int unit = UNIT_PIXELS;

    private SWF swf;

    private MainPanel mainPanel;

    public HeaderInfoPanel(MainPanel mainPanel) {
        setLayout(new BorderLayout());
        this.mainPanel = mainPanel;

        TableLayout tl;
        propertiesPanel.setLayout(tl = new TableLayout(new double[][]{
            {TableLayout.PREFERRED, TableLayout.FILL},
            {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
                TableLayout.PREFERRED, TableLayout.PREFERRED}
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

        encryptedCheckBox.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });

        gfxCheckBox.addChangeListener((ChangeEvent e) -> {
            validateHeader();
        });

        frameRateEditorPanel.setLayout(layout);
        frameRateEditor.setPreferredSize(new Dimension(80, frameRateEditor.getPreferredSize().height));
        frameRateEditorPanel.add(frameRateEditor);

        frameCountEditorPanel.setLayout(layout);
        frameCountEditor.setPreferredSize(new Dimension(80, frameCountEditor.getPreferredSize().height));
        frameCountEditorPanel.add(frameCountEditor);

        JLabel frameCountWarningIcon = new JLabel(View.getIcon("warning16"));
        frameCountWarningIcon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        frameCountWarningIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        frameCountWarningIcon.setToolTipText(AppStrings.translate("warning.icon"));
        frameCountWarningIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    ViewMessages.showMessageDialog(HeaderInfoPanel.this,
                            AppStrings.translate("warning.edit.headerframecount"), AppStrings.translate("message.warning"), JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        frameCountEditorPanel.add(frameCountWarningIcon);

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

        unitComboBox = new JComboBox<>(new String[]{
            AppStrings.translate("header.displayrect.unit.pixels"),
            AppStrings.translate("header.displayrect.unit.twips")
        });
        displayRectEditorPanel.add(unitComboBox);

        unitComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int newUnit = unitComboBox.getSelectedIndex();
                    double multiplier = 1.0;
                    if (unit == UNIT_PIXELS && newUnit == UNIT_TWIPS) {
                        multiplier = 20.0;
                    }
                    if (unit == UNIT_TWIPS && newUnit == UNIT_PIXELS) {
                        multiplier = 1 / 20.0;
                    }
                    unit = newUnit;

                    xMinEditor.setValue(((double) xMinEditor.getValue()) * multiplier);
                    yMinEditor.setValue(((double) yMinEditor.getValue()) * multiplier);
                    xMaxEditor.setValue(((double) xMaxEditor.getValue()) * multiplier);
                    yMaxEditor.setValue(((double) yMaxEditor.getValue()) * multiplier);
                }
            }
        });

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
        propertiesPanel.add(new JLabel(AppStrings.translate("header.encrypted")), "0,3");
        propertiesPanel.add(encryptedLabel, "1,3");
        propertiesPanel.add(encryptedCheckBox, "1,3");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.gfx")), "0,4");
        propertiesPanel.add(gfxLabel, "1,4");
        propertiesPanel.add(gfxCheckBox, "1,4");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.filesize")), "0,5");
        propertiesPanel.add(fileSizeLabel, "1,5");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framerate")), "0,6");
        propertiesPanel.add(frameRateLabel, "1,6");
        propertiesPanel.add(frameRateEditorPanel, "1,6");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.framecount")), "0,7");
        propertiesPanel.add(frameCountLabel, "1,7");
        propertiesPanel.add(frameCountEditorPanel, "1,7");
        propertiesPanel.add(new JLabel(AppStrings.translate("header.displayrect")), "0,8");
        propertiesPanel.add(displayRectTwipsLabel, "1,8");
        propertiesPanel.add(displayRectEditorPanel, "1,8");
        propertiesPanel.add(new JLabel(""), "0,9");
        propertiesPanel.add(displayRectPixelsLabel, "1,9");
        propertiesPanel.add(warningPanel, "0,10,1,10");

        add(propertiesPanel, BorderLayout.CENTER);

        editButton.addActionListener(this::editButtonActionPerformed);

        saveButton.addActionListener(this::saveButtonActionPerformed);

        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        if (Configuration.editorMode.get()) {
            editButton.setVisible(false);
            saveButton.setVisible(false);
            saveButton.setEnabled(false);
            cancelButton.setVisible(false);
            cancelButton.setEnabled(false);
        } else {
            editButton.setVisible(false);
            saveButton.setVisible(false);
            cancelButton.setVisible(false);
        }

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
        mainPanel.setEditingStatus();
    }

    private void saveButtonActionPerformed(ActionEvent evt) {
        swf.compression = getCompression();
        swf.version = getVersionNumber();
        swf.gfx = gfxCheckBox.isSelected();
        swf.encrypted = encryptedCheckBox.isSelected() && !gfxCheckBox.isSelected();
        swf.frameRate = ((Number) (frameRateEditor.getModel().getValue())).floatValue();
        swf.frameCount = ((Number) (frameCountEditor.getModel().getValue())).intValue();
        double multiplier = 1.0;
        if (unit == UNIT_PIXELS) {
            multiplier = 20.0;
        }
        swf.displayRect.Xmin = (int) (multiplier * (double) xMinEditor.getModel().getValue());
        swf.displayRect.Xmax = (int) (multiplier * (double) xMaxEditor.getModel().getValue());
        swf.displayRect.Ymin = (int) (multiplier * (double) yMinEditor.getModel().getValue());
        swf.displayRect.Ymax = (int) (multiplier * (double) yMaxEditor.getModel().getValue());
        swf.setHeaderModified(true);

        load(swf);
        mainPanel.repaintTree();
        setEditMode(false);
        mainPanel.clearEditingStatus();
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        load(swf);
        setEditMode(false);
        mainPanel.clearEditingStatus();
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

        encryptedLabel.setText(swf.encrypted ? AppStrings.translate("yes") : AppStrings.translate("no"));
        encryptedCheckBox.setSelected(swf.encrypted);

        gfxLabel.setText(swf.gfx ? AppStrings.translate("yes") : AppStrings.translate("no"));
        gfxCheckBox.setSelected(swf.gfx);

        fileSizeLabel.setText(Long.toString(swf.fileSize));

        frameRateLabel.setText(Float.toString(swf.frameRate));
        frameRateEditor.setModel(new SpinnerNumberModel(swf.frameRate, -0x80000000, 0x7fffffff, 1));

        frameCountLabel.setText("" + swf.frameCount);
        frameCountEditor.setModel(new SpinnerNumberModel(swf.frameCount, -0x80000000, 0x7fffffff, 1));

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

        double multiplier = 1.0;
        if (unit == UNIT_PIXELS) {
            multiplier = 1 / SWF.unitDivisor;
        }

        xMinEditor.setModel(new SpinnerNumberModel(swf.displayRect.Xmin * multiplier, -0x80000000, 0x7fffffff, 1));
        xMaxEditor.setModel(new SpinnerNumberModel(swf.displayRect.Xmax * multiplier, -0x80000000, 0x7fffffff, 1));
        yMinEditor.setModel(new SpinnerNumberModel(swf.displayRect.Ymin * multiplier, -0x80000000, 0x7fffffff, 1));
        yMaxEditor.setModel(new SpinnerNumberModel(swf.displayRect.Ymax * multiplier, -0x80000000, 0x7fffffff, 1));

        compressionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setModified();
            }
        });
        versionEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        encryptedCheckBox.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        gfxCheckBox.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        frameRateEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        xMinEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        xMaxEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        yMinEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });
        yMaxEditor.addChangeListener((ChangeEvent e) -> {
            setModified();
        });

        setEditMode(Configuration.editorMode.get());
    }

    private void setModified() {
        saveButton.setEnabled(true);
        cancelButton.setEnabled(true);
        mainPanel.setEditingStatus();
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

        if (Configuration.editorMode.get()) {
            edit = true;
        }
        compressionLabel.setVisible(!edit);
        compressionEditorPanel.setVisible(edit);
        versionLabel.setVisible(!edit);
        versionEditorPanel.setVisible(edit);
        encryptedLabel.setVisible(!edit);
        encryptedCheckBox.setVisible(edit);
        gfxLabel.setVisible(!edit);
        gfxCheckBox.setVisible(edit);
        frameRateLabel.setVisible(!edit);
        frameRateEditorPanel.setVisible(edit);
        frameCountLabel.setVisible(!edit);
        frameCountEditorPanel.setVisible(edit);

        displayRectTwipsLabel.setVisible(!edit);
        displayRectPixelsLabel.setVisible(!edit);
        displayRectEditorPanel.setVisible(edit);

        warningPanel.setVisible(false);

        if (Configuration.editorMode.get()) {
            editButton.setVisible(false);
            saveButton.setVisible(true);
            saveButton.setEnabled(false);
            cancelButton.setVisible(true);
            cancelButton.setEnabled(false);
        } else {
            editButton.setVisible(!edit);
            saveButton.setVisible(edit);
            cancelButton.setVisible(edit);
        }
    }

    private boolean validateHeader() {
        int version = getVersionNumber();
        boolean gfx = gfxCheckBox.isSelected();
        boolean encrypted = encryptedCheckBox.isSelected();
        SWFCompression compression = getCompression();
        String resultStr = "";
        boolean result = true;
        if (gfx && !(compression == SWFCompression.NONE || compression == SWFCompression.ZLIB)) {
            resultStr += AppStrings.translate("header.warning.unsupportedGfxCompression") + " ";
            result = false;
        }

        if (gfx && encrypted) {
            resultStr += AppStrings.translate("header.warning.unsupportedGfxEncryption") + " ";
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
        if (saveButton.isVisible() && saveButton.isEnabled() && Configuration.autoSaveTagModifications.get()) {
            saveButtonActionPerformed(null);
        }
        return !(saveButton.isVisible() && saveButton.isEnabled());
    }

    @Override
    public boolean isEditing() {
        return saveButton.isVisible() && saveButton.isEnabled();
    }

    public void startEdit() {
        if (!editButton.isVisible()) {
            return;
        }
        editButtonActionPerformed(null);
    }
}
