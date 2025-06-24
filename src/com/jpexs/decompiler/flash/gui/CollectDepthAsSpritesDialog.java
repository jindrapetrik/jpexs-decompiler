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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * @author JPEXS
 */
public class CollectDepthAsSpritesDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JList<Integer> depthsList;

    private final JCheckBox replaceCheckBox;

    private final JCheckBox offsetCheckBox;

    private final JCheckBox firstMatrixCheckBox;

    private int result = ERROR_OPTION;

    public CollectDepthAsSpritesDialog(Window owner) {
        super(owner);
        setSize(400, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));
        cnt.add(new JLabel(translate("collect.depths")));

        depthsList = new JList<>();
        depthsList.setVisibleRowCount(7);
        depthsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane listScroller = new FasterScrollPane(depthsList);
        listScroller.setPreferredSize(new Dimension(400, 200));
        cnt.add(listScroller);

        replaceCheckBox = new JCheckBox(translate("collect.replace"));
        cnt.add(replaceCheckBox);

        offsetCheckBox = new JCheckBox(translate("collect.offset"));
        cnt.add(offsetCheckBox);

        firstMatrixCheckBox = new JCheckBox(translate("collect.matrix"));
        cnt.add(firstMatrixCheckBox);

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        add(panButtons, BorderLayout.SOUTH);

        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        setTitle(translate("dialog.title"));
        getRootPane().setDefaultButton(okButton);
        pack();
        View.centerScreen(this);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public List<Integer> getDepths() {
        if (result == ERROR_OPTION) {
            return null;
        }

        return depthsList.getSelectedValuesList();
    }

    public boolean getReplace() {
        return replaceCheckBox.isSelected();
    }

    public boolean getOffset() {
        return offsetCheckBox.isSelected();
    }

    public boolean getEnsureFirstMatrix() {
        return firstMatrixCheckBox.isSelected();
    }

    public int showDialog(Collection<Integer> depths) {
        depthsList.setListData(depths.toArray(new Integer[depths.size()]));
        depthsList.setVisibleRowCount(7);
        setVisible(true);
        return result;
    }
}
