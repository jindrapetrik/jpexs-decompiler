/*
 *  Copyright (C) 2021-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.SelectTagOfTypeDialog;
import com.jpexs.decompiler.flash.gui.SelectTagPositionDialog;
import com.jpexs.decompiler.flash.gui.View;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.treeitems.Openable;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author JPEXS
 */
public class AddClassDialog extends AppDialog {

    private final JButton proceedButton = new JButton(translate("button.proceed"));
    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JTextField classNameTextField = new JTextField(30);
    private String selectedClass = null;
    private ABCContainerTag selectedAbcContainer;
    private Tag selectedPosition;
    private Timelined selectedTimelined;
    private int result = ERROR_OPTION;

    private Openable openable;

    private int abcCount = 0;

    private ABCContainerTag preselectedAbcContainer;

    private JRadioButton existingAbcTagRadioButton = new JRadioButton(translate("abc.where.existing"));
    private JRadioButton newAbcTagRadioButton = new JRadioButton(translate("abc.where.new"));

    public AddClassDialog(Window owner, Openable openable, ABCContainerTag abcContainer) {
        super(owner);
        this.openable = openable;
        abcCount = 0;
        this.preselectedAbcContainer = abcContainer;
        if (abcContainer != null) {
            abcCount = 1;
        } else if (openable instanceof SWF) {
            SWF swf = (SWF) openable;
            for (Tag t : swf.getTags()) {
                if (t instanceof ABCContainerTag) {
                    abcCount++;
                }
            }
        } else {
            abcCount = 1;
        }

        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        JPanel abcTargetPanel = new JPanel();
        abcTargetPanel.setLayout(new BoxLayout(abcTargetPanel, BoxLayout.Y_AXIS));
        abcTargetPanel.add(new JLabel(translate("abc.where")));
        existingAbcTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        newAbcTagRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                checkEnabled();
            }
        });
        ButtonGroup abcTargetButtonGroup = new ButtonGroup();
        abcTargetButtonGroup.add(existingAbcTagRadioButton);
        abcTargetButtonGroup.add(newAbcTagRadioButton);
        existingAbcTagRadioButton.setSelected(true);
        abcTargetPanel.add(existingAbcTagRadioButton);
        abcTargetPanel.add(newAbcTagRadioButton);

        if (abcCount == 0) {
            newAbcTagRadioButton.setSelected(true);
            abcTargetPanel.setVisible(false);
        }

        if (preselectedAbcContainer != null) {
            existingAbcTagRadioButton.setSelected(true);
            abcTargetPanel.setVisible(false);
        }

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        proceedButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(proceedButton);
        buttonsPanel.add(cancelButton);

        JLabel classNameLabel = new JLabel(translate("classname"));
        classNameLabel.setAlignmentX(JLabel.CENTER);
        cnt.add(classNameLabel);
        cnt.add(classNameTextField);

        cnt.add(abcTargetPanel);

        cnt.add(buttonsPanel);

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }
        });

        classNameTextField.addActionListener(this::okButtonActionPerformed);

        pack();
        setModal(true);
        setResizable(false);
        View.setWindowIcon(this);
        View.centerScreen(this);
        checkEnabled();
    }

    private void checkEnabled() {

        if (existingAbcTagRadioButton.isSelected() && abcCount == 1) {
            proceedButton.setText(translate("button.ok"));
        } else {
            proceedButton.setText(translate("button.proceed"));
        }

        boolean ok = true;

        if (classNameTextField.getText().isEmpty()) {
            ok = false;
        }

        if (classNameTextField.getText().endsWith(".")) {
            ok = false;
        }

        if (ok) {

            if (openable instanceof SWF) {
                SWF swf = (SWF) openable;
                List<String> classNames = new ArrayList<>();
                classNames.add(classNameTextField.getText());
                try {
                    List<ScriptPack> scriptPacks = swf.getScriptPacksByClassNames(classNames);
                    if (!scriptPacks.isEmpty()) {
                        ok = false;
                    }
                } catch (Exception ex) {
                    ok = false;
                }
            }
            if (openable instanceof ABC) {
                ABC abc = (ABC) openable;
                List<ABC> allAbcs = new ArrayList<>();
                allAbcs.add(abc);
                try {
                    List<ScriptPack> scriptPacks = abc.findScriptPacksByPath(classNameTextField.getText(), allAbcs);
                    if (!scriptPacks.isEmpty()) {
                        ok = false;
                    }
                } catch (Exception ex) {
                    ok = false;
                }
            }
        }

        proceedButton.setEnabled(ok);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (!proceedButton.isEnabled()) {
            return;
        }
        setVisible(false);
        if (preselectedAbcContainer != null) {
            selectedAbcContainer = preselectedAbcContainer;
        } else {
            if (existingAbcTagRadioButton.isSelected()) {
                SelectTagOfTypeDialog selectDoABCDialog = new SelectTagOfTypeDialog(owner, (SWF) openable, ABCContainerTag.class, "DoABC", 1);
                selectedAbcContainer = (ABCContainerTag) selectDoABCDialog.showDialog();
                if (selectedAbcContainer == null) {
                    cancelButtonActionPerformed(evt);
                    return;
                }
            }
            if (newAbcTagRadioButton.isSelected()) {
                SelectTagPositionDialog selectTagPositionDialog = new SelectTagPositionDialog(owner, (SWF) openable, true);
                if (selectTagPositionDialog.showDialog() != OK_OPTION) {
                    cancelButtonActionPerformed(evt);
                    return;
                }
                selectedPosition = selectTagPositionDialog.getSelectedTag();
                selectedTimelined = selectTagPositionDialog.getSelectedTimelined();
            }
        }

        result = OK_OPTION;
        selectedClass = classNameTextField.getText();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        selectedClass = null;
        selectedAbcContainer = null;
        selectedPosition = null;
        selectedTimelined = null;
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        return showDialog("");
    }

    public int showDialog(String pkg) {
        classNameTextField.setText(pkg);
        selectedClass = null;
        selectedAbcContainer = null;
        selectedPosition = null;
        selectedTimelined = null;
        setVisible(true);
        return result;
    }

    public Tag getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public Timelined getSelectedTimelined() {
        return selectedTimelined;
    }

    public ABCContainerTag getSelectedAbcContainer() {
        return selectedAbcContainer;
    }
}
