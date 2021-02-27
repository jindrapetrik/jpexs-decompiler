/*
 *  Copyright (C) 2021 JPEXS
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

import static com.jpexs.decompiler.flash.gui.AppDialog.CANCEL_OPTION;
import static com.jpexs.decompiler.flash.gui.AppDialog.ERROR_OPTION;
import static com.jpexs.decompiler.flash.gui.AppDialog.OK_OPTION;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author JPEXS
 */
public class FilesChangedDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private JList<String> filesList;

    private DefaultListModel<String> listModel;

    private boolean onTopInited = false;

    public FilesChangedDialog(Window parent) {
        super(parent);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                clearList();
            }

        });

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        JLabel label = new JLabel(translate("fileschanged"));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        cnt.add(label, BorderLayout.NORTH);
        filesList = new JList<String>(listModel);
        filesList.setBackground(Color.white);
        cnt.add(new FasterScrollPane(filesList), BorderLayout.CENTER);

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        add(panButtons, BorderLayout.SOUTH);

        View.setWindowIcon(this);
        setResizable(true);
        pack();
        View.centerScreen(this);
    }

    public void addItem(String item) {
        if (listModel.contains(item)) {
            return;
        }
        listModel.addElement(item);
        filesList.setModel(listModel);
    }

    @Override
    public void setVisible(boolean b) {
        if (b && !onTopInited) {
            Main.getMainFrame().getWindow().addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) {
                    Window wnd = e.getOppositeWindow();
                    if (wnd != FilesChangedDialog.this) {
                        FilesChangedDialog.this.setAlwaysOnTop(false);
                    }
                }

                @Override
                public void windowActivated(WindowEvent e) {
                    if (!FilesChangedDialog.this.isAlwaysOnTop()) {
                        FilesChangedDialog.this.setAlwaysOnTop(true);
                    }
                }
            });

            addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) {
                    if (e.getOppositeWindow() == null) {
                        setAlwaysOnTop(false);
                    }
                }
            });
            onTopInited = true;
        }
        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listModel.size(); i++) {
                    String fileName = listModel.elementAt(i);
                    Main.reloadFile(new File(fileName));
                }
            }
        });

    }

    private void clearList() {
        listModel.clear();
        filesList.setModel(listModel);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        clearList();
        setVisible(false);
    }
}
