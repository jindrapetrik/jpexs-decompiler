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

import com.jpexs.decompiler.flash.tags.converters.ShapeTypeConverter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author JPEXS
 */
public class ConvertPlaceObjectTypeDialog extends AppDialog {

    private List<JRadioButton> radios = new ArrayList<>();

    private int result = 0;

    public ConvertPlaceObjectTypeDialog(Window owner, int currentPlaceObjectNum, int min) {
        super(owner);
        setTitle(translate("dialog.title"));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));
        ButtonGroup radioGroup = new ButtonGroup();

        JButton okButton = new JButton(translate("button.ok"));

        for (int i = 1; i <= 4; i++) {
            String text = "PlaceObject" + (i > 1 ? "" + i : "");
            if (i == min) {
                text += " " + translate("minimum");
            }
            text += " - " + translate("place" + i);
            JRadioButton radio = new JRadioButton(text);
            radio.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (i == currentPlaceObjectNum) {
                radio.setSelected(true);
            }
            final int fi = i;
            radio.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    okButton.setEnabled(fi != currentPlaceObjectNum);
                }
            });
            radioPanel.add(radio);
            radioGroup.add(radio);
            radios.add(radio);
        }

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        cnt.add(radioPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout());

        okButton.setEnabled(false);
        okButton.addActionListener(this::okButtonActionPerformed);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        pack();
        View.centerScreen(this);
        View.setWindowIcon(this, "placeobject");
        setModal(true);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = 0;
        for (int i = 0; i < radios.size(); i++) {
            if (radios.get(i).isSelected()) {
                result = i + 1;
                break;
            }
        }
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    public int getResult() {
        return result;
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}
