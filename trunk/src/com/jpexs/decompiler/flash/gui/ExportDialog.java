/*
 *  Copyright (C) 2010-2013 JPEXS
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class ExportDialog extends AppDialog {

    boolean cancelled = false;
    String[][] options = {
        {translate("shapes.svg")},
        {translate("texts.plain"), translate("texts.formatted")},
        {translate("images.pngjpeg")},
        {translate("movies.flv")},
        {translate("sounds.mp3wavflv"), translate("sounds.flv")},
        {translate("actionscript.as"), translate("actionscript.pcode"), translate("actionscript.pcodehex"), translate("actionscript.hex")}
    };
    String[] optionNames = {
        translate("shapes"),
        translate("texts"),
        translate("images"),
        translate("movies"),
        translate("sounds"),
        translate("actionscript")
    };
    public static final int OPTION_SHAPES = 0;
    public static final int OPTION_TEXTS = 1;
    public static final int OPTION_IMAGES = 2;
    public static final int OPTION_MOVIES = 3;
    public static final int OPTION_SOUNDS = 4;
    public static final int OPTION_ACTIONSCRIPT = 5;
    private final JComboBox[] combos;

    public int getOption(int index) {
        return combos[index].getSelectedIndex();
    }

    public ExportDialog() {
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
        JLabel[] labels = new JLabel[optionNames.length];
        int labWidth = 0;
        for (int i = 0; i < optionNames.length; i++) {
            labels[i] = new JLabel(optionNames[i]);
            if (labels[i].getPreferredSize().width > labWidth) {
                labWidth = labels[i].getPreferredSize().width;
            }
        }
        int comboWidth = 200;
        int top = 10;
        for (int i = 0; i < optionNames.length; i++) {
            JLabel lab = new JLabel(optionNames[i]);
            lab.setBounds(10, top, lab.getPreferredSize().width, lab.getPreferredSize().height);
            comboPanel.add(lab);
            combos[i] = new JComboBox<>(options[i]);
            combos[i].setBounds(10 + labWidth + 10, top, comboWidth, combos[i].getPreferredSize().height);
            comboPanel.add(combos[i]);
            top += combos[i].getHeight();
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
        //setSize(245, top + getInsets().top);
        View.centerScreen(this);
        View.setWindowIcon(this);
        getRootPane().setDefaultButton(okButton);
        setModal(true);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            cancelled = false;
        }
        super.setVisible(b);
    }
}
