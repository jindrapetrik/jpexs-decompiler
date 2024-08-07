/*
 *  Copyright (C) 2010-2024 JPEXS
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
import com.jpexs.decompiler.flash.exporters.modes.Font4ExportMode;
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
import com.jpexs.decompiler.flash.tags.DefineFont4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.BinaryDataInterface;
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
import com.jpexs.decompiler.flash.treeitems.AS3ClassTreeItem;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
        "fonts4",
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
        {BinaryDataInterface.class},
        {Frame.class},
        {Frame.class},
        {ButtonTag.class},
        {FontTag.class},
        {DefineFont4Tag.class},
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
        Font4ExportMode.class,
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

    private JCheckBox embedCheckBox;

    private JCheckBox resampleWavCheckBox;

    private JCheckBox transparentFrameBackgroundCheckBox;

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

    public boolean isEmbedEnabled() {
        return embedCheckBox.isSelected();
    }

    public boolean isResampleWavEnabled() {
        return resampleWavCheckBox.isSelected();
    }

    public boolean isTransparentFrameBackgroundEnabled() {
        return transparentFrameBackgroundCheckBox.isSelected();
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
        if (embedCheckBox.isVisible()) {
            Configuration.lastExportEnableEmbed.set(embedCheckBox.isSelected());
        }
        if (resampleWavCheckBox.isVisible()) {
            Configuration.lastExportResampleWav.set(resampleWavCheckBox.isSelected());
        }
        if (transparentFrameBackgroundCheckBox.isVisible()) {
            Configuration.lastExportTransparentBackground.set(transparentFrameBackgroundCheckBox.isSelected());
        }
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

    public ExportDialog(Window owner, List<TreeItem> exportables) {
        super(owner);
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

        List<Class> visibleOptionClasses = new ArrayList<>();

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

            visibleOptionClasses.add(c);

            JLabel lab = new JLabel(translate(optionNames[i]));
            lab.setBounds(10, top, lab.getPreferredSize().width, lab.getPreferredSize().height);
            comboPanel.add(lab);
            comboPanel.add(checkBoxes[i]);
            comboPanel.add(combos[i]);
            top += combos[i].getHeight();
        }

        embedCheckBox = new JCheckBox(translate("embed"));
        embedCheckBox.setVisible(false);

        boolean hasAs3 = false;
        if (exportables == null) {
            hasAs3 = true; //??
        } else {
            for (TreeItem ti : exportables) {
                if (ti instanceof AS3ClassTreeItem) {
                    hasAs3 = true;
                    break;
                }
            }
        }

        int w = 10 + labWidth + 10 + checkBoxWidth + 10 + comboWidth + 10;

        if (hasAs3 && visibleOptionClasses.contains(ScriptExportMode.class)) {
            embedCheckBox.setVisible(true);
            top += 2;
            embedCheckBox.setBounds(10, top, embedCheckBox.getPreferredSize().width, embedCheckBox.getPreferredSize().height);
            comboPanel.add(embedCheckBox);
            top += embedCheckBox.getHeight();

            if (embedCheckBox.getWidth() + 10 > w) {
                w = embedCheckBox.getWidth() + 10;
            }
            if (Configuration.lastExportEnableEmbed.get()) {
                embedCheckBox.setSelected(true);
            }
        }

        resampleWavCheckBox = new JCheckBox(translate("resampleWav"));
        resampleWavCheckBox.setVisible(false);

        if (embedCheckBox.isVisible() || visibleOptionClasses.contains(SoundExportMode.class)) {
            top += 2;
            resampleWavCheckBox.setVisible(true);
            comboPanel.add(resampleWavCheckBox);
            if (Configuration.lastExportResampleWav.get()) {
                resampleWavCheckBox.setSelected(true);
            }

            resampleWavCheckBox.setBounds(10, top, resampleWavCheckBox.getPreferredSize().width, resampleWavCheckBox.getPreferredSize().height);
            top += resampleWavCheckBox.getHeight();

            if (resampleWavCheckBox.getWidth() + 10 > w) {
                w = resampleWavCheckBox.getWidth() + 10;
            }
        }

        transparentFrameBackgroundCheckBox = new JCheckBox(translate("transparentFrameBackground"));
        transparentFrameBackgroundCheckBox.setVisible(false);
        if (visibleOptionClasses.contains(FrameExportMode.class)) {
            top += 2;
            transparentFrameBackgroundCheckBox.setVisible(true);
            comboPanel.add(transparentFrameBackgroundCheckBox);
            if (Configuration.lastExportTransparentBackground.get()) {
                transparentFrameBackgroundCheckBox.setSelected(true);
            }

            transparentFrameBackgroundCheckBox.setBounds(10, top, transparentFrameBackgroundCheckBox.getPreferredSize().width, transparentFrameBackgroundCheckBox.getPreferredSize().height);
            top += transparentFrameBackgroundCheckBox.getHeight();

            if (transparentFrameBackgroundCheckBox.getWidth() + 10 > w) {
                w = transparentFrameBackgroundCheckBox.getWidth() + 10;
            }
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

        Dimension dim = new Dimension(w, top + 10);
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
        View.setWindowIcon(this, "export");
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
