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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
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

    private static final String[] DEVELOPERS = new String[]{
        "JPEXS",
        "honfika",
        "others"
    };

    private static final String AUTHOR = "JPEXS";

    public AboutDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        //setSize(new Dimension(300, 320));
        setTitle(translate("dialog.title"));

        DEVELOPERS[DEVELOPERS.length - 1] = translate("developers.others"); // translate "others" text

        JPanel twoPanes = new JPanel();
        twoPanes.setLayout(new BoxLayout(twoPanes, BoxLayout.X_AXIS));

        Container cnt = getContentPane();
        JPanel cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cnt.setLayout(new BorderLayout());

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

        cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
        cp.add(appNamePanel);
        JLabel verLabel = new JLabel(translate("version") + " " + ApplicationInfo.version);
        verLabel.setAlignmentX(0.5f);
        //verLabel.setPreferredSize(new Dimension(300, 15));
        verLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        verLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(verLabel);

        cnt.add(cp, BorderLayout.NORTH);

        cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

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

        JLabel dateLabel = new JLabel("2010-2018");
        dateLabel.setAlignmentX(0.5f);
        //dateLabel.setPreferredSize(new Dimension(300, 10));
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(dateLabel);

        LinkLabel wwwLabel = new LinkLabel(ApplicationInfo.PROJECT_PAGE, ApplicationInfo.PROJECT_PAGE);
        wwwLabel.setAlignmentX(0.5f);
        wwwLabel.setForeground(Color.blue);
        //wwwLabel.setPreferredSize(new Dimension(300, 25));
        wwwLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(wwwLabel);

        cp.add(Box.createVerticalStrut(20));
        JLabel transAuthorLabel = new JLabel(translate("translation.author.label"));
        transAuthorLabel.setAlignmentX(0.5f);
        //transAuthorLabel.setPreferredSize(new Dimension(300, 20));
        transAuthorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel transAuthor = new JLabel(translate("translation.author"));
        transAuthor.setAlignmentX(0.5f);
        transAuthor.setHorizontalAlignment(SwingConstants.CENTER);
        cp.add(transAuthorLabel);
        cp.add(transAuthor);
        cp.add(Box.createVerticalStrut(50));
        JLabel developersLabel = new JLabel(translate("developers"));
        developersLabel.setAlignmentX(0.5f);
        developersLabel.setHorizontalAlignment(SwingConstants.CENTER);
        developersLabel.setFont(developersLabel.getFont().deriveFont(Font.BOLD));
        cp.add(developersLabel);

        for (String c : DEVELOPERS) {
            JLabel developerNameLabel = new JLabel(c);
            developerNameLabel.setAlignmentX(0.5f);
            developerNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            cp.add(developerNameLabel);
        }

        cp.setAlignmentY(0);
        twoPanes.add(cp);
        cp = new JPanel();
        cp.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));

        JLabel translatorsLabel = new JLabel(translate("translators"));
        translatorsLabel.setAlignmentX(0.5f);
        translatorsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        translatorsLabel.setFont(translatorsLabel.getFont().deriveFont(Font.BOLD));
        cp.add(translatorsLabel);

        List<String> translators = new ArrayList<>();
        for (String code : SelectLanguageDialog.languages) {
            Locale l = Locale.forLanguageTag(code.equals("en") ? "" : code);
            ResourceBundle b = ResourceBundle.getBundle(AppStrings.getResourcePath(AboutDialog.class), l);
            translators.add(Locale.forLanguageTag(code).getDisplayName() + " - " + b.getString("translation.author"));
        }
        for (String c : translators) {
            JLabel translatorName = new JLabel(c);
            translatorName.setAlignmentX(0.5f);
            translatorName.setHorizontalAlignment(SwingConstants.CENTER);
            cp.add(translatorName);
        }

        cp.add(Box.createVerticalStrut(10));
        cp.setAlignmentY(0);
        twoPanes.add(cp);

        cnt.add(twoPanes, BorderLayout.CENTER);
        cp = new JPanel(new FlowLayout());
        cnt.add(cp, BorderLayout.SOUTH);
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
        View.setWindowIcon(this);
        setResizable(false);
        pack();
        View.centerScreen(this);
    }
}
