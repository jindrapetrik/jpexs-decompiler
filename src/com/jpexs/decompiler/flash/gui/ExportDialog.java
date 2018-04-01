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

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ButtonExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FrameExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SpriteExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SymbolClassExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.ButtonTag;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.SymbolClassTypeTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.timeline.TagScript;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
public class ExportDialog extends AppDialog {

    private int result = ERROR_OPTION;

    String[] optionNames = {
        TagTreeModel.FOLDER_SHAPES,
        TagTreeModel.FOLDER_TEXTS,
        TagTreeModel.FOLDER_IMAGES,
        TagTreeModel.FOLDER_MOVIES,
        TagTreeModel.FOLDER_SOUNDS,
        TagTreeModel.FOLDER_SCRIPTS,
        TagTreeModel.FOLDER_BINARY_DATA,
        TagTreeModel.FOLDER_FRAMES,
        TagTreeModel.FOLDER_SPRITES,
        TagTreeModel.FOLDER_BUTTONS,
        TagTreeModel.FOLDER_FONTS,
        TagTreeModel.FOLDER_MORPHSHAPES,
        "symbolclass"
    };

    //Display options only when these classes found
    Class[][] objClasses = {
        {ShapeTag.class},
        {TextTag.class},
        {ImageTag.class},
        {DefineVideoStreamTag.class},
        {SoundTag.class},
        {ASMSource.class, ScriptPack.class, TagScript.class},
        {DefineBinaryDataTag.class},
        {Frame.class},
        {Frame.class},
        {ButtonTag.class},
        {FontTag.class},
        {MorphShapeTag.class},
        {SymbolClassTypeTag.class}
    };

    //Enum classes for values
    Class[] optionClasses = {
        ShapeExportMode.class,
        TextExportMode.class,
        ImageExportMode.class,
        MovieExportMode.class,
        SoundExportMode.class,
        ScriptExportMode.class,
        BinaryDataExportMode.class,
        FrameExportMode.class,
        SpriteExportMode.class,
        ButtonExportMode.class,
        FontExportMode.class,
        MorphShapeExportMode.class,
        SymbolClassExportMode.class
    };

    Class[] zoomClasses = {
        ShapeExportMode.class,
        MorphShapeExportMode.class,
        TextExportMode.class,
        FrameExportMode.class,
        SpriteExportMode.class,
        ButtonExportMode.class
    };

    private final JComboBox[] combos;

    private final JCheckBox[] checkBoxes;

    private final JCheckBox selectAllCheckBox;

    private JTextField zoomTextField = new JTextField();

    public <E> E getValue(Class<E> option) {
        for (int i = 0; i < optionClasses.length; i++) {
            if (option == optionClasses[i]) {
                E[] values = option.getEnumConstants();
                return values[combos[i].getSelectedIndex()];
            }
        }

        return null;
    }

    public boolean isOptionEnabled(Class<?> option) {
        for (int i = 0; i < optionClasses.length; i++) {
            if (option == optionClasses[i]) {
                return checkBoxes[i].isSelected();
            }
        }

        return false;
    }

    public double getZoom() {
        return Double.parseDouble(zoomTextField.getText()) / 100;
    }

    private void saveConfig() {
        StringBuilder cfg = new StringBuilder();
        for (int i = 0; i < optionNames.length; i++) {
            int selIndex = combos[i].getSelectedIndex();
            Class c = optionClasses[i];
            Object[] vals = c.getEnumConstants();
            String key = optionNames[i] + "." + vals[selIndex].toString().toLowerCase(Locale.ENGLISH);
            if (i > 0) {
                cfg.append(",");
            }
            cfg.append(key);
        }

        Configuration.lastSelectedExportZoom.set(Double.parseDouble(zoomTextField.getText()) / 100);
        Configuration.lastSelectedExportFormats.set(cfg.toString());
    }

    private boolean optionCanHandle(int optionIndex, Object e) {
        for (int i = 0; i < objClasses[optionIndex].length; i++) {
            Class c = objClasses[optionIndex][i];
            if (c.isInstance(e)) {
                if (c == Frame.class) { //Frame class can be SWF frame or Sprite frame
                    Frame f = (Frame) e;
                    boolean isSprite = (f.timeline.timelined instanceof DefineSpriteTag);
                    boolean spritesWanted = optionClasses[optionIndex] == SpriteExportMode.class;
                    if (spritesWanted == isSprite) { //both true or both false
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public ExportDialog(List<TreeItem> exportables) {
        setTitle(translate("dialog.title"));
        setResizable(false);

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JPanel comboPanel = new JPanel(null);
        int labWidth = 0;
        boolean[] exportableExistsArray = new boolean[optionNames.length];
        for (int i = 0; i < optionNames.length; i++) {
            boolean exportableExists = false;
            if (exportables == null) {
                exportableExists = true;
            } else {
                for (TreeItem e : exportables) {
                    if (optionCanHandle(i, e)) {
                        exportableExists = true;
                    }
                }
            }

            if (!exportableExists) {
                continue;
            }

            exportableExistsArray[i] = true;

            JLabel label = new JLabel(translate(optionNames[i]));
            if (label.getPreferredSize().width > labWidth) {
                labWidth = label.getPreferredSize().width;
            }
        }

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

        int comboWidth = 200;
        int checkBoxWidth;
        int top = 10;

        List<String> exportFormats = Arrays.asList(exportFormatsArr);
        combos = new JComboBox[optionNames.length];
        checkBoxes = new JCheckBox[optionNames.length];
        selectAllCheckBox = new JCheckBox();
        checkBoxWidth = selectAllCheckBox.getPreferredSize().width;
        selectAllCheckBox.setBounds(10 + labWidth + 10 + comboWidth + 10, top, checkBoxWidth, selectAllCheckBox.getPreferredSize().height);
        selectAllCheckBox.setSelected(true);
        selectAllCheckBox.addActionListener((ActionEvent e) -> {
            boolean selected = selectAllCheckBox.isSelected();
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox != null) {
                    checkBox.setSelected(selected);
                }
            }
        });
        comboPanel.add(selectAllCheckBox);
        top += selectAllCheckBox.getHeight();

        boolean zoomable = false;
        for (int i = 0; i < optionNames.length; i++) {
            Class c = optionClasses[i];
            Object[] vals = c.getEnumConstants();
            String[] names = new String[vals.length];
            int itemIndex = -1;
            for (int j = 0; j < vals.length; j++) {

                String key = optionNames[i] + "." + vals[j].toString().toLowerCase(Locale.ENGLISH);
                if (exportFormats.contains(key)) {
                    itemIndex = j;
                }
                names[j] = translate(key);
            }

            combos[i] = new JComboBox<>(names);
            if (itemIndex > -1) {
                combos[i].setSelectedIndex(itemIndex);
            }

            combos[i].setBounds(10 + labWidth + 10, top, comboWidth, combos[i].getPreferredSize().height);

            checkBoxes[i] = new JCheckBox();
            checkBoxes[i].setBounds(10 + labWidth + 10 + comboWidth + 10, top, checkBoxWidth, checkBoxes[i].getPreferredSize().height);
            checkBoxes[i].setSelected(true);

            if (!exportableExistsArray[i]) {
                continue;
            }

            if (Arrays.asList(zoomClasses).contains(c)) {
                zoomable = true;
            }

            JLabel lab = new JLabel(translate(optionNames[i]));
            lab.setBounds(10, top, lab.getPreferredSize().width, lab.getPreferredSize().height);
            comboPanel.add(lab);
            comboPanel.add(checkBoxes[i]);
            comboPanel.add(combos[i]);
            top += combos[i].getHeight();
        }

        int zoomWidth = 50;
        if (zoomable) {
            top += 2;
            JLabel zlab = new JLabel(translate("zoom"));
            zlab.setBounds(10, top + 4, zlab.getPreferredSize().width, zlab.getPreferredSize().height);
            zoomTextField.setBounds(10 + labWidth + 10, top, zoomWidth, zoomTextField.getPreferredSize().height);
            JLabel pctLabel = new JLabel(translate("zoom.percent"));
            pctLabel.setBounds(10 + labWidth + 10 + zoomWidth + 5, top + 4, 20, pctLabel.getPreferredSize().height);

            comboPanel.add(zlab);
            comboPanel.add(zoomTextField);
            comboPanel.add(pctLabel);
            top += zoomTextField.getHeight();
        }

        Dimension dim = new Dimension(10 + labWidth + 10 + checkBoxWidth + 10 + comboWidth + 10, top + 10);
        comboPanel.setMinimumSize(dim);
        comboPanel.setPreferredSize(dim);
        cnt.add(comboPanel, BorderLayout.CENTER);

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
        View.setWindowIcon(this);
        getRootPane().setDefaultButton(okButton);
        setModal(true);
        String pct = "" + Configuration.lastSelectedExportZoom.get() * 100;
        if (pct.endsWith(".0")) {
            pct = pct.substring(0, pct.length() - 2);
        }

        zoomTextField.setText(pct);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
        }
        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        try {
            saveConfig();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(ExportDialog.this, translate("zoom.invalid"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            zoomTextField.requestFocusInWindow();
            return;
        }

        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showExportDialog() {
        setVisible(true);
        return result;
    }
}
