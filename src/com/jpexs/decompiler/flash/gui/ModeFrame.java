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

import com.jpexs.decompiler.flash.ApplicationInfo;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * Frame with selection on application startServer
 *
 * @author JPEXS
 */
public class ModeFrame extends AppFrame {

    private final JButton openButton = new JButton(translate("button.open"));

    private final JButton proxyButton = new JButton(translate("button.proxy"));

    private final JButton exitButton = new JButton(translate("button.exit"));

    /**
     * Constructor
     */
    public ModeFrame() {
        setSize(350, 200);
        openButton.addActionListener(this::openButtonActionPerformed);
        openButton.setIcon(View.getIcon("open32"));
        proxyButton.addActionListener(this::proxyButtonActionPerformed);
        proxyButton.setIcon(View.getIcon("proxy32"));
        exitButton.addActionListener(this::exitButtonActionPerformed);
        exitButton.setIcon(View.getIcon("exit32"));
        setResizable(false);
        Container cont = getContentPane();
        cont.setLayout(new GridLayout(4, 1));
        JLabel logoLabel = new JLabel();
        logoLabel.setIcon(View.getIcon("logo"));
        cont.add(logoLabel);
        cont.add(openButton);
        cont.add(proxyButton);
        cont.add(exitButton);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setTitle(ApplicationInfo.shortApplicationVerName);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Main.exit();
            }
        });
    }

    private void openButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
        if (!Main.openFileDialog()) {
            setVisible(true);
        }
    }

    private void proxyButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
        Main.showProxy();
    }

    private void exitButtonActionPerformed(ActionEvent evt) {
        setVisible(false);
        Main.exit();
    }
}
