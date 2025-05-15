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
package com.jpexs.decompiler.flash.gui.soleditor;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.amf.amf0.Amf0Exporter;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.gui.AppFrame;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.FasterScrollPane;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.gui.ViewMessages;
import com.jpexs.decompiler.flash.gui.editor.LineMarkedEditorPane;
import com.jpexs.decompiler.flash.importers.amf.AmfParseException;
import com.jpexs.decompiler.flash.importers.amf.amf0.Amf0Importer;
import com.jpexs.decompiler.flash.importers.amf.amf3.Amf3Importer;
import com.jpexs.decompiler.flash.sol.SolFile;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author JPEXS
 */
public class SolEditorFrame extends AppFrame {

    private File openedFile = null;
    private boolean modified = false;

    private final JButton saveAsButton;
    private final JButton saveButton;
    private JTextField fileNameField = new JTextField(30);
    private JComboBox<Integer> amfVersionComboBox = new JComboBox<>();
    private JLabel amfVersionLabel = new JLabel();

    private LineMarkedEditorPane editor = new LineMarkedEditorPane();

    private final DocumentListener modifiedListener;

    public SolEditorFrame(boolean exitOnClose) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(translate("dialog.title"));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (modified && ViewMessages.showConfirmDialog(SolEditorFrame.this, translate("warning.loseChanges"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
                    return;
                }
                if (exitOnClose) {
                    System.exit(0);
                } else {
                    setVisible(false);
                }
            }
        });

        modifiedListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified();
            }

            private void setModified() {
                if (modified) {
                    return;
                }
                modified = true;
                updateTitle();
            }
        };

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton newButton = new JButton(translate("button.new"), View.getIcon("newswf16"));
        newButton.addActionListener(this::newActionPerformed);

        JButton openButton = new JButton(translate("button.open"), View.getIcon("open16"));
        openButton.addActionListener(this::openActionPerformed);

        JButton openNpApiButton = new JButton(translate("button.open.npapi"), View.getIcon("open16"));
        openNpApiButton.addActionListener(this::openNpApiActionPerformed);

        JButton openPpApiButton = new JButton(translate("button.open.ppapi"), View.getIcon("open16"));
        openPpApiButton.addActionListener(this::openPpApiActionPerformed);

        saveButton = new JButton(translate("button.save"), View.getIcon("save16"));
        saveButton.addActionListener(this::saveActionPerformed);
        saveButton.setEnabled(false);

        saveAsButton = new JButton(translate("button.saveAs"), View.getIcon("saveas16"));
        saveAsButton.addActionListener(this::saveAsActionPerformed);

        topPanel.add(newButton);
        topPanel.add(openButton);

        File npApiDirectory = SharedObjectsStorage.getNpApiDirectory();
        if (npApiDirectory != null && npApiDirectory.exists()) {
            topPanel.add(openNpApiButton);
        }
        File ppApiDirectory = SharedObjectsStorage.getPpApiDirectory();
        if (ppApiDirectory != null && ppApiDirectory.exists()) {
            topPanel.add(openPpApiButton);
        }
        topPanel.add(saveButton);
        topPanel.add(saveAsButton);

        fileNameField.setText(translate("untitled"));
        fileNameField.getDocument().addDocumentListener(modifiedListener);
        DefaultComboBoxModel<Integer> amfVersionModel = new DefaultComboBoxModel<>();
        amfVersionModel.addElement(0);
        amfVersionModel.addElement(3);
        amfVersionComboBox.setModel(amfVersionModel);
        amfVersionComboBox.setSelectedIndex(1);
        amfVersionLabel.setText("" + amfVersionComboBox.getSelectedItem());
        amfVersionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                amfVersionLabel.setText(amfVersionComboBox.getSelectedItem().toString());
            }
        });

        amfVersionLabel.setVisible(false);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(new JLabel(translate("filename")));
        bottomPanel.add(fileNameField);
        bottomPanel.add(Box.createHorizontalStrut(20));
        bottomPanel.add(new JLabel(translate("amfVersion")));
        bottomPanel.add(amfVersionComboBox);
        bottomPanel.add(amfVersionLabel);

        cnt.add(new FasterScrollPane(editor), BorderLayout.CENTER);
        cnt.add(topPanel, BorderLayout.NORTH);
        cnt.add(bottomPanel, BorderLayout.SOUTH);
        editor.setText("{\r\n\r\n}");
        setSize(800, 600);

        View.centerScreen(this);
        View.setWindowIcon(this, "soleditor");
    }

    private void newActionPerformed(ActionEvent e) {
        if (modified && ViewMessages.showConfirmDialog(this, translate("warning.loseChanges"), AppStrings.translate("message.warning"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        openedFile = null;
        editor.setText("{\r\n\r\n}");
        modified = false;
        amfVersionComboBox.setEnabled(true);
        amfVersionComboBox.setSelectedItem(3);
        amfVersionLabel.setVisible(false);
        amfVersionComboBox.setVisible(true);
        updateTitle();
        fileNameField.setText(translate("untitled"));
    }

    private void openActionPerformed(ActionEvent e) {
        openDirectory(new File(Configuration.lastSolEditorDirectory.get()));
    }

    private void openNpApiActionPerformed(ActionEvent e) {
        openDirectory(SharedObjectsStorage.getNpApiDirectory());
    }

    private void openPpApiActionPerformed(ActionEvent e) {
        openDirectory(SharedObjectsStorage.getPpApiDirectory());
    }

    private void openDirectory(File directory) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sol");
            }

            @Override
            public String getDescription() {
                return translate("filter.sol");
            }
        });
        fileChooser.setCurrentDirectory(directory);
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File newFile = Helper.fixDialogFile(fileChooser.getSelectedFile());

        try (FileInputStream fis = new FileInputStream(newFile)) {
            SolFile solFile = new SolFile(fis);
            Map<String, Object> values = solFile.getAmfValues();
            String newFileName = solFile.getFileName();
            int newAmfVersion = solFile.getAmfVersion();
            switch (newAmfVersion) {
                case 0:
                    editor.setText(Amf0Exporter.amfMapToString(values, 0, "\r\n"));
                    break;
                case 3:
                    editor.setText(Amf3Exporter.amfMapToString(values, "  ", "\r\n", 0));
                    break;
                default:
                    throw new IllegalArgumentException("No AMF version found");
            }
            fileNameField.setText(newFileName);
            amfVersionComboBox.setSelectedItem(newAmfVersion);
            amfVersionLabel.setText("" + newAmfVersion);
        } catch (IOException | IllegalArgumentException ex) {
            ViewMessages.showMessageDialog(this, translate("error.cannotOpen") + " " + ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        modified = false;
        openedFile = newFile;
        updateTitle();
        Configuration.lastSolEditorDirectory.set(newFile.getParentFile().getAbsolutePath());
        saveButton.setEnabled(true);
        saveAsButton.setEnabled(true);
        amfVersionComboBox.setEnabled(false);
        amfVersionComboBox.setVisible(false);
        amfVersionLabel.setVisible(true);
    }

    private void updateTitle() {
        setTitle(translate("dialog.title") + (openedFile != null ? " - " + (modified ? "*" : "") + openedFile.getAbsolutePath() : ""));
    }

    private void saveActionPerformed(ActionEvent e) {
        if (openedFile == null) {
            saveAsActionPerformed(e);
            return;
        }
        saveAs(openedFile);
    }

    private void saveAs(File saveFile) {
        try {
            String amfText = editor.getText();
            String fileName = fileNameField.getText();
            int amfVersion = (Integer) amfVersionComboBox.getSelectedItem();
            Map<String, Object> amfValues;
            switch (amfVersion) {
                case 0:
                    Amf0Importer amf0Importer = new Amf0Importer();
                    amfValues = amf0Importer.stringToAmfMap(amfText);
                    break;
                case 3:
                    Amf3Importer amf3Importer = new Amf3Importer();
                    amfValues = amf3Importer.stringToAmfMap(amfText);
                    break;
                default:
                    return; //should not happen
            }
            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                SolFile solFile = new SolFile(fileName, amfVersion, amfValues);
                solFile.writeTo(fos);
            }
            openedFile = saveFile;
            Configuration.lastSolEditorDirectory.set(saveFile.getParentFile().getAbsolutePath());
            modified = false;
            updateTitle();
            amfVersionComboBox.setEnabled(false);
            amfVersionComboBox.setVisible(false);
            amfVersionLabel.setVisible(true);
            ViewMessages.showMessageDialog(this, translate("info.saved"), AppStrings.translate("message.info"), JOptionPane.INFORMATION_MESSAGE);
        } catch (AmfParseException ex) {
            editor.gotoLine((int) ex.line);
            editor.markError();
            ViewMessages.showMessageDialog(this, translate("error.parse").replace("%reason%", ex.text).replace("%line%", "" + ex.line), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ViewMessages.showMessageDialog(this, ex.getLocalizedMessage(), AppStrings.translate("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAsActionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileHidingEnabled(false);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sol");
            }

            @Override
            public String getDescription() {
                return translate("filter.sol");
            }
        });
        fileChooser.setCurrentDirectory(new File(Configuration.lastSolEditorDirectory.get()));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File newFile = Helper.fixDialogFile(fileChooser.getSelectedFile());
        saveAs(newFile);
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            editor.setContentType("text/javascript");
            editor.setText("{\r\n\r\n}");
            editor.getDocument().addDocumentListener(modifiedListener);
        }
    }
}
