/*
 *  Copyright (C) 2011-2013 JPEXS
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class AboutDialog extends AppDialog {

    public AboutDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(300, 320));
        setTitle(translate("dialog.title"));


        Container cp = getContentPane();
        cp.setLayout(new FlowLayout());

        JLabel jpLabel = new JLabel("JPEXS");
        jpLabel.setForeground(new Color(0, 0, 160));
        jpLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        jpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(jpLabel);

        JLabel asLabel = new JLabel("Free Flash");
        asLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        asLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(asLabel);

        JLabel decLabel = new JLabel("Decompiler");
        decLabel.setForeground(Color.red);
        decLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(decLabel);

        JLabel verLabel = new JLabel(translate("version") + " " + Main.version);
        verLabel.setPreferredSize(new Dimension(300, 15));
        verLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        verLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(verLabel);


        JLabel byLabel = new JLabel(translate("by"));
        byLabel.setPreferredSize(new Dimension(300, 15));
        byLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(byLabel);

        JLabel jpexsLabel = new JLabel("JPEXS");
        jpexsLabel.setForeground(new Color(0, 0, 160));
        jpexsLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        jpexsLabel.setPreferredSize(new Dimension(300, 25));
        jpexsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(jpexsLabel);

        JLabel dateLabel = new JLabel("2010-2013");
        dateLabel.setPreferredSize(new Dimension(300, 10));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(dateLabel);

        LinkLabel wwwLabel = new LinkLabel(Main.projectPage);
        wwwLabel.setForeground(Color.blue);
        wwwLabel.setPreferredSize(new Dimension(300, 25));
        wwwLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(wwwLabel);


        JLabel transAuthorLabel = new JLabel(translate("translation.author.label"));
        transAuthorLabel.setPreferredSize(new Dimension(300, 20));
        transAuthorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel transAuthor = new JLabel(translate("translation.author"));
        transAuthor.setPreferredSize(new Dimension(300, 20));
        transAuthor.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(transAuthorLabel);
        cp.add(transAuthor);
        JButton okButton = new JButton(translate("button.ok"));
        cp.add(okButton);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        getRootPane().setDefaultButton(okButton);
        setModal(true);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setResizable(false);
    }
}
