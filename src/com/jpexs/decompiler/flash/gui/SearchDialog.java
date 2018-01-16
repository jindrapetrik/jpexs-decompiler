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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class SearchDialog extends AppDialog {

    public JTextField searchField = new MyTextField();

    public JTextField replaceField = new MyTextField();

    public JCheckBox ignoreCaseCheckBox = new JCheckBox(translate("checkbox.ignorecase"));

    public JCheckBox regexpCheckBox = new JCheckBox(translate("checkbox.regexp"));

    public JCheckBox replaceInParametersCheckBox = new JCheckBox(translate("checkbox.replaceInParameters"));

    public JRadioButton searchInASRadioButton = new JRadioButton(translate("checkbox.searchAS"));

    public JRadioButton searchInPCodeRadioButton = new JRadioButton(translate("checkbox.searchPCode"));

    public JRadioButton searchInTextsRadioButton = new JRadioButton(translate("checkbox.searchText"));

    private int result = ERROR_OPTION;

    public SearchDialog(Window owner, boolean replace) {
        super(owner);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        ignoreCaseCheckBox.setSelected(true);
        Container cnt = getContentPane();
        setSize(400, 150);
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.PAGE_AXIS));
        JPanel panButtons = new JPanel(new FlowLayout());
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);
        JPanel panField = new JPanel(new FlowLayout());
        searchField.setPreferredSize(new Dimension(250, searchField.getPreferredSize().height));
        panField.add(new JLabel(translate("label.searchtext")));
        panField.add(searchField);
        cnt.add(panField);

        if (replace) {
            panField = new JPanel(new FlowLayout());
            replaceField.setPreferredSize(new Dimension(250, replaceField.getPreferredSize().height));
            panField.add(new JLabel(translate("label.replacementtext")));
            panField.add(replaceField);
            cnt.add(panField);
        }

        JPanel checkPanel = new JPanel(new FlowLayout());
        checkPanel.add(ignoreCaseCheckBox);
        checkPanel.add(regexpCheckBox);
        if (replace) {
            checkPanel.add(replaceInParametersCheckBox);
        }

        cnt.add(checkPanel);

        if (!replace) {
            ButtonGroup group = new ButtonGroup();
            group.add(searchInASRadioButton);
            group.add(searchInPCodeRadioButton);
            group.add(searchInTextsRadioButton);

            JPanel rbPanel = new JPanel(new FlowLayout());
            searchInASRadioButton.setSelected(true);
            searchInPCodeRadioButton.setSelected(false);
            searchInTextsRadioButton.setSelected(false);
            rbPanel.add(searchInASRadioButton);
            rbPanel.add(searchInPCodeRadioButton);
            rbPanel.add(searchInTextsRadioButton);
            cnt.add(rbPanel);
        }

        cnt.add(panButtons);
        getRootPane().setDefaultButton(okButton);
        View.centerScreen(this);
        setIconImage(View.loadImage(replace ? "replace16" : "search16"));
        setTitle(replace ? translate("dialog.title.replace") : translate("dialog.title"));
        setModalityType(ModalityType.APPLICATION_MODAL);
        pack();
        List<Image> images = new ArrayList<>();
        images.add(View.loadImage(replace ? "replace16" : "search16"));
        images.add(View.loadImage(replace ? "replace32" : "search32"));
        setIconImages(images);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
            searchField.requestFocusInWindow();
        }

        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        if (regexpCheckBox.isSelected()) {
            try {
                Pattern pat = Pattern.compile(searchField.getText());
            } catch (PatternSyntaxException ex) {
                View.showMessageDialog(null, translate("error.invalidregexp"), translate("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
