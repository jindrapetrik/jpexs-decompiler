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
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class AboutDialog extends AppDialog {

    private static final String[] CONTRIBUTORS = new String[]{
        "Paolo Cancedda",
        "Capasha",
        "focus",
        "honfika",
        "pepka"};
    private static final String AUTHOR = "JPEXS";

    public AboutDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(300, 320));
        setTitle(translate("dialog.title"));


        Container cnt = getContentPane();
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cnt.setLayout(new BorderLayout());
        cnt.add(cp, BorderLayout.CENTER);
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

        JPanel appNamePanel = new JPanel(new FlowLayout());
        JLabel jpLabel = new JLabel("JPEXS");
        jpLabel.setAlignmentX(0.5f);
        jpLabel.setForeground(new Color(0, 0, 160));
        jpLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        jpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(jpLabel);

        JLabel ffLabel = new JLabel("Free Flash");
        ffLabel.setAlignmentX(0.5f);
        ffLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        ffLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(ffLabel);

        JLabel decLabel = new JLabel("Decompiler");
        decLabel.setAlignmentX(0.5f);
        decLabel.setForeground(Color.red);
        decLabel.setFont(new Font("Tahoma", Font.BOLD, 25));
        decLabel.setHorizontalAlignment(SwingConstants.CENTER);
        appNamePanel.add(decLabel);
        appNamePanel.setAlignmentX(0.5f);
        cp.add(appNamePanel);

        JLabel verLabel = new JLabel(translate("version") + " " + Main.version);
        verLabel.setAlignmentX(0.5f);
        //verLabel.setPreferredSize(new Dimension(300, 15));
        verLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        verLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(verLabel);


        JLabel byLabel = new JLabel(translate("by"));
        byLabel.setAlignmentX(0.5f);
        byLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(byLabel);

        JLabel authorLabel = new JLabel(AUTHOR);
        authorLabel.setAlignmentX(0.5f);
        authorLabel.setForeground(new Color(0, 0, 160));
        authorLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
        //jpexsLabel.setPreferredSize(new Dimension(300, 25));
        authorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(authorLabel);




        JLabel dateLabel = new JLabel("2010-2013");
        dateLabel.setAlignmentX(0.5f);
        //dateLabel.setPreferredSize(new Dimension(300, 10));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(dateLabel);




        JLabel transAuthorLabel = new JLabel(translate("translation.author.label"));
        transAuthorLabel.setAlignmentX(0.5f);
        //transAuthorLabel.setPreferredSize(new Dimension(300, 20));
        transAuthorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel transAuthor = new JLabel(translate("translation.author"));
        transAuthor.setAlignmentX(0.5f);
        //transAuthor.setPreferredSize(new Dimension(300, 20));
        transAuthor.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(transAuthorLabel);
        cp.add(transAuthor);


        JLabel contributorsLabel = new JLabel(translate("contributors"));
        contributorsLabel.setAlignmentX(0.5f);
        contributorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contributorsLabel.setFont(contributorsLabel.getFont().deriveFont(Font.BOLD));
        cp.add(contributorsLabel);

        for (String c : CONTRIBUTORS) {
            JLabel contributorLabel = new JLabel(c);
            contributorLabel.setAlignmentX(0.5f);
            contributorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cp.add(contributorLabel);
        }

        cp.add(Box.createVerticalStrut(10));

        LinkLabel wwwLabel = new LinkLabel(Main.projectPage);
        wwwLabel.setAlignmentX(0.5f);
        wwwLabel.setForeground(Color.blue);
        //wwwLabel.setPreferredSize(new Dimension(300, 25));
        wwwLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(wwwLabel);
        cp.add(Box.createVerticalStrut(10));
        JButton okButton = new JButton(translate("button.ok"));
        okButton.setAlignmentX(0.5f);
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
        pack();
    }
}
