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

import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.BinaryDataExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.exporters.modes.FramesExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ImageExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MorphShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.MovieExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.modes.TextExportMode;
import com.jpexs.decompiler.flash.gui.tagtree.TagTreeModel;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineVideoStreamTag;
import com.jpexs.decompiler.flash.tags.base.ASMSource;
import com.jpexs.decompiler.flash.tags.base.FontTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.TextTag;
import com.jpexs.decompiler.flash.timeline.Frame;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
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

    boolean cancelled = false;

    String[] optionNames = {
        TagTreeModel.FOLDER_SHAPES,
        TagTreeModel.FOLDER_TEXTS,
        TagTreeModel.FOLDER_IMAGES,
        TagTreeModel.FOLDER_MOVIES,
        TagTreeModel.FOLDER_SOUNDS,
        TagTreeModel.FOLDER_SCRIPTS,
        TagTreeModel.FOLDER_BINARY_DATA,
        TagTreeModel.FOLDER_FRAMES,
        TagTreeModel.FOLDER_FONTS,
        TagTreeModel.FOLDER_MORPHSHAPES
    };

    //Display options only when these classes found
    Class[][] objClasses = {
        {ShapeTag.class},
        {TextTag.class},
        {ImageTag.class},
        {DefineVideoStreamTag.class},
        {SoundTag.class},
        {ASMSource.class, ScriptPack.class},
        {DefineBinaryDataTag.class},
        {Frame.class},
        {FontTag.class},
        {MorphShapeTag.class}
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
        FramesExportMode.class,
        FontExportMode.class,
        MorphShapeExportMode.class
    };

    Class[] zoomClasses = {
        ShapeExportMode.class,
        TextExportMode.class,
        FramesExportMode.class,
        MorphShapeExportMode.class
    };

    private final JComboBox[] combos;
    private JTextField zoomTextField = new JTextField();

    public <E> E getValue(Class<E> option) {
        for (int i = 0; i < optionClasses.length; i++) {
            if (option == optionClasses[i]) {
                E values[] = option.getEnumConstants();
                return values[combos[i].getSelectedIndex()];
            }
        }
        return null;
    }

    public double getZoom() {
        return Double.parseDouble(zoomTextField.getText()) / 100;
    }

    private void saveConfig() {
        String cfg = "";
        for (int i = 0; i < optionNames.length; i++) {
            int selIndex = combos[i].getSelectedIndex();
            Class c = optionClasses[i];
            Object vals[] = c.getEnumConstants();
            String key = optionNames[i] + "." + vals[selIndex].toString().toLowerCase();
            if (i > 0) {
                cfg += ",";
            }
            cfg += key;
        }
        Configuration.lastSelectedExportZoom.set(Double.parseDouble(zoomTextField.getText()) / 100);
        Configuration.lastSelectedExportFormats.set(cfg);
    }

    public ExportDialog(List<TreeItem> exportables) {
        setTitle(translate("dialog.title"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelled = true;
            }
        });

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        JPanel comboPanel = new JPanel(null);
        combos = new JComboBox[optionNames.length];
        int labWidth = 0;
        for (int i = 0; i < optionNames.length; i++) {
            JLabel label = new JLabel(optionNames[i]);
            if (label.getPreferredSize().width > labWidth) {
                labWidth = label.getPreferredSize().width;
            }
        }
        String exportFormatsStr = Configuration.lastSelectedExportFormats.get();
        if ("".equals(exportFormatsStr)) {
            exportFormatsStr = null;
        }
        String exportFormatsArr[] = new String[0];
        if (exportFormatsStr != null) {
            if (exportFormatsStr.contains(",")) {
                exportFormatsArr = exportFormatsStr.split(",");
            } else {
                exportFormatsArr = new String[]{exportFormatsStr};
            }

        }
        List<String> exportFormats = Arrays.asList(exportFormatsArr);
        int comboWidth = 200;
        int top = 10;
        boolean zoomable = false;
        for (int i = 0; i < optionNames.length; i++) {
            Class c = optionClasses[i];
            Object vals[] = c.getEnumConstants();
            String names[] = new String[vals.length];
            int itemIndex = -1;
            for (int j = 0; j < vals.length; j++) {

                String key = optionNames[i] + "." + vals[j].toString().toLowerCase();
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
            boolean exportableExists = false;
            if (exportables == null) {
                exportableExists = true;
            } else {
                for (TreeItem e : exportables) {
                    for (int j = 0; j < objClasses[i].length; j++) {
                        if (objClasses[i][j].isInstance(e)) {
                            exportableExists = true;
                        }
                    }
                }
            }
            if (!exportableExists) {
                continue;
            }
            if (Arrays.asList(zoomClasses).contains(c)) {
                zoomable = true;
            }
            JLabel lab = new JLabel(translate(optionNames[i]));
            lab.setBounds(10, top, lab.getPreferredSize().width, lab.getPreferredSize().height);
            comboPanel.add(lab);
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

        Dimension dim = new Dimension(10 + labWidth + 10 + comboWidth + 10, top + 10);
        comboPanel.setMinimumSize(dim);
        comboPanel.setPreferredSize(dim);
        cnt.add(comboPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    saveConfig();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(ExportDialog.this, translate("zoom.invalid"), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
                    zoomTextField.requestFocusInWindow();
                    return;
                }
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                setVisible(false);
            }
        });

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
            cancelled = false;
        }
        super.setVisible(b);
    }
}
